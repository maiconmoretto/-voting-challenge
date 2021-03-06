package com.br;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.br.model.Voting;
import com.br.repository.VotingRepository;
import org.springframework.boot.test.context.SpringBootTest;


@RunWith(SpringJUnit4ClassRunner.class)
public class VotingRepositoryTests {

	@Mock
	private VotingRepository votingRepository;

	private Voting voting;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		voting = new Voting();
	}

	@Test
	public void findAll() {
		List<Voting> votingList = new ArrayList<Voting>();
		votingList.add(new Voting());
		votingList.add(new Voting());
		votingList.add(new Voting());
		when(votingRepository.findAll()).thenReturn(votingList);

		List<Voting> result = (List<Voting>) votingRepository.findAll();
		assertEquals(3, result.size());
	}

	@Test
	public void deleteById() {
		votingRepository.deleteById(voting.getId());
		verify(votingRepository, times(1)).deleteById(voting.getId());
	}

	@Test
	public void save() {
		Voting voting = new Voting();
		when(votingRepository.save(voting)).thenReturn(voting);
		Voting result = votingRepository.save(voting);
		assertEquals("01-01-01 01:01:01", result.getCreatedAt());
		assertEquals(1, result.getIdUser());
		assertEquals(1, result.getIdAgenda());
		assertEquals("Sim", result.getVote());
	}
	
	@Test
	public void update() {
		when(votingRepository.save(voting)).thenReturn(voting);
		Voting result = votingRepository.save(voting);
		assertEquals("01-01-01 01:01:01", result.getCreatedAt());
		assertEquals(1, result.getIdUser());
		assertEquals(1, result.getIdAgenda());
		assertEquals("Sim", result.getVote());
	}

	@Test
	public void findById() {
		Optional<Voting> voting = Optional.of(new Voting());
		when(votingRepository.findById(1)).thenReturn(voting);
		Optional<Voting> result = votingRepository.findById(1);
		assertEquals("01-01-01 01:01:01", result.get().getCreatedAt());
		assertEquals(1, result.get().getIdUser());
		assertEquals(1, result.get().getIdAgenda());
		assertEquals("Sim", result.get().getVote());
	}
}
