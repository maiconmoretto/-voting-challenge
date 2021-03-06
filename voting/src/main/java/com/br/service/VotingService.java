package com.br.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.br.model.UserDTO;
import com.br.model.AgendaDTO;
import com.br.model.Voting;
import feign.FeignException;
import org.springframework.web.server.ResponseStatusException;
import com.br.request.VotingRequest;
import com.br.AppException;
import com.br.model.Voting;
import com.br.repository.VotingRepository;
import org.springframework.web.client.RestTemplate;
import java.util.Optional;
import com.br.service.UserServiceClient;
import com.br.request.VotingRequestUpdate;

@Service
public class VotingService {

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AgendaServiceClient agendaServiceClient;

    public List<Voting> findAll() {
        return (List<Voting>) votingRepository.findAll();
    }

    public Voting findById(int id) {
        return votingRepository.findById(id).get();
    }

    public ResponseEntity<String> save(VotingRequest votingRequest) throws ResponseStatusException {
        Voting voting = new Voting();
        voting.setVote(votingRequest.getVote());
        voting.setIdUser(votingRequest.getIdUser());
        voting.setIdAgenda(votingRequest.getIdAgenda());

        boolean validateUpdate = false;
        this.validate(voting, validateUpdate);

        AgendaDTO agendaDTO;

        try {
            agendaDTO = agendaServiceClient.findById(voting.getIdAgenda());
            if (voting.getVote().equals("Sim")) {
                agendaDTO.setSim(agendaDTO.getSim() + 1);
            } else {
                agendaDTO.setNao(agendaDTO.getNao() + 1);
            }
            agendaServiceClient.update(agendaDTO);
        } catch (FeignException ex) {
            if (HttpStatus.NOT_FOUND.value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No Agenda found with id: " + voting.getIdAgenda()
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "System unavaible." + HttpStatus.NOT_FOUND.value()
                ) ;
            }
        }

        votingRepository.save(voting);

        return new ResponseEntity<>("Voting successfully registered", HttpStatus.CREATED);
    }

    public void validate(Voting voting,boolean validateUpdate) {
        UserDTO userDTO;
        AgendaDTO agendaDTO;

        try {
            userDTO = userServiceClient.findById(voting.getIdUser());
        } catch (FeignException ex) {
            if (HttpStatus.NOT_FOUND.value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No user found with the id: " + voting.getIdUser()
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "System unavaible." + HttpStatus.NOT_FOUND.value()
                ) ;
            }
        }

        try {
            agendaDTO = agendaServiceClient.findById(voting.getIdAgenda());
        } catch (FeignException ex) {
            if (HttpStatus.NOT_FOUND.value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No Agenda found with id: " + voting.getIdAgenda()
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "System unavaible." + HttpStatus.NOT_FOUND.value()
                ) ;
            }
        }

        try {
            AgendaDTO agendaOpen = agendaServiceClient.agendaOpen(voting.getIdAgenda());
            if (agendaOpen == null) {
                throw new AppException(404, "This agenda is already closed for voting");
            }
        } catch (FeignException ex) {
            if (HttpStatus.NOT_FOUND.value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Error due find if agenda is open: " + voting.getIdAgenda()
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "System unavaible." + HttpStatus.NOT_FOUND.value()
                ) ;
            }
        }

        if (!voting.getVote().equals("Sim") && !voting.getVote().equals("Não")) {
            throw new AppException(404, "The vote is only Sim or Não");
        }

        if(!validateUpdate) {
            Optional<Voting> userAlreadyVote = votingRepository.userAlreadyVote(voting.getIdAgenda(), voting.getIdUser());

            if (userAlreadyVote.isPresent()) {
                throw new AppException(404, "This user already voted");
            }
        }
    }

    public ResponseEntity<String> update(VotingRequestUpdate votingRequestUpdate) {
        boolean validateUpdate = true;
        Voting voting = new Voting();
        voting.setVote(votingRequestUpdate.getVote());
        voting.setIdAgenda(votingRequestUpdate.getIdAgenda());
        voting.setIdUser(votingRequestUpdate.getIdUser());
        this.validate(voting, validateUpdate);
        AgendaDTO agendaDTO;

        try {
            agendaDTO = agendaServiceClient.findById(voting.getIdAgenda());
            if (voting.getVote().equals("Sim")) {
                agendaDTO.setSim(agendaDTO.getSim() + 1);
                agendaDTO.setNao(agendaDTO.getNao() - 1);
            } else {
                agendaDTO.setNao(agendaDTO.getNao() + 1);
                agendaDTO.setSim(agendaDTO.getSim() - 1);
            }
            agendaServiceClient.update(agendaDTO);
        } catch (FeignException ex) {
            if (HttpStatus.NOT_FOUND.value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No Agenda found with id: " + voting.getIdAgenda()
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "System unavaible." + HttpStatus.NOT_FOUND.value()
                ) ;
            }
        }

        Optional<Voting> voteOld = votingRepository.findByIdUser(voting.getIdUser());
        System.out.println(voteOld.toString());
        Voting newVote = new Voting();
        newVote.setVote(voting.getVote());
        newVote.setId(voteOld.get().getId());
        newVote.setIdAgenda(voteOld.get().getIdAgenda());
        newVote.setIdUser(voteOld.get().getIdUser());

		votingRepository.save(newVote);
        return new ResponseEntity<>("Voting successfully updated", HttpStatus.CREATED);
    }

    public ResponseEntity<String> deleteById(int id) {
        votingRepository.deleteById(id);
        return new ResponseEntity<>("Voting successfully deleted", HttpStatus.CREATED);
    }
}
