package com.project.main.service.user;

import com.project.main.dto.user.UserPreferenceDto;
import com.project.main.dto.user.UserPreferenceUpdateRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.repository.cases.TagRepository;
import com.project.main.repository.projection.TagWithCountRow;
import com.project.main.repository.user.UserPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPreferenceServiceTest {

    @Mock private UserPreferenceRepository preferenceRepository;
    @Mock private TagRepository tagRepository;

    private UserPreferenceService service() {
        return new UserPreferenceService(preferenceRepository, tagRepository);
    }

    @Test
    void getPreferencesReturnsEmptyDtoWhenNothingSaved() {
        when(preferenceRepository.findByUserId(9L)).thenReturn(Optional.empty());

        UserPreferenceDto dto = service().getPreferences(9L);

        assertThat(dto.getUserId()).isEqualTo(9L);
        assertThat(dto.getPreferredDifficulty()).isNull();
        assertThat(dto.getPreferredTags()).isEmpty();
    }

    @Test
    void updatePreferencesRejectsMissingOrInactiveTags() {
        TagWithCountRow onlyOne = mock(TagWithCountRow.class);
        when(tagRepository.findTagsWithCaseCountByIds(List.of(1L, 2L)))
                .thenReturn(List.of(onlyOne)); // вернули 1 строку вместо 2

        UserPreferenceUpdateRequest request =
                new UserPreferenceUpdateRequest(null, List.of(1L, 2L), false, false);

        assertThatThrownBy(() -> service().updatePreferences(request, 9L))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("One or more tags are invalid or inactive");
    }
}