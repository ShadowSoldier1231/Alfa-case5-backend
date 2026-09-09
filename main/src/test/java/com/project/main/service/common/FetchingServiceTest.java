package com.project.main.service.common;

import com.project.main.dto.common.PageResponse;
import com.project.main.exception.BadRequestException;
import com.project.main.model.common.City;
import com.project.main.repository.common.CityRepository;
import com.project.main.repository.user.LeaderboardRepository;
import com.project.main.repository.user.UserDataRepository;
import com.project.main.repository.user.UserRepository;
import com.project.main.service.component.TypeMapperComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FetchingServiceTest {

    @Mock private LeaderboardRepository leaderboardRepository;
    @Mock private UserDataRepository userDataRepository;
    @Mock private CityRepository cityRepository;
    @Mock private UserRepository userRepository;

    private FetchingService service() {
        return new FetchingService(leaderboardRepository, userDataRepository,
                cityRepository, userRepository, new TypeMapperComponent());
    }

    @Test
    void cityExistsByIdRejectsNullAndMinusOneWithoutTouchingRepository() {
        assertThat(service().cityExistsById(null)).isFalse();
        assertThat(service().cityExistsById(-1L)).isFalse();

        verifyNoInteractions(cityRepository);
    }

    @Test
    void searchCitiesRejectsNegativePage() {
        assertThatThrownBy(() -> service().searchCities("моск", -1, 25, "city_name,asc"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Page cannot be negative");
    }

    @Test
    void searchCitiesReturnsEmptyPageForBlankQuery() {
        PageResponse<City> result = service().searchCities("   ", 0, 25, null);

        assertThat(result.getItems()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}