package be.volley.live;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.volley.live.repository.GameRepository;
import be.volley.live.repository.ScoreRepository;
import be.volley.live.repository.TeamRepository;

@SpringBootTest
@ActiveProfiles("test")
class VolleyLiveScoresApplicationTests {

	@MockitoBean
	TeamRepository teamRepository;

	@MockitoBean
	GameRepository gameRepository;

	@MockitoBean
	ScoreRepository scoreRepository;

	@Test
	void contextLoads() {
	}

}
