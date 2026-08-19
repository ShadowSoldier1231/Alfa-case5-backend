package com.project.main.service;


import com.project.main.dto.TagListItem;
import com.project.main.dto.UserPreferenceDto;
import com.project.main.dto.UserPreferenceUpdateRequest;
import com.project.main.exception.BadRequestException;
import com.project.main.model.UserPreference;
import com.project.main.repository.TagRepository;
import com.project.main.repository.UserPreferenceRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserPreferenceService {

    private final UserPreferenceRepository preferenceRepository;
    private final TagRepository tagRepository;

    public UserPreferenceService(UserPreferenceRepository preferenceRepository,
                                 TagRepository tagRepository){
        this.preferenceRepository = preferenceRepository;
        this.tagRepository = tagRepository;

    }

    public UserPreferenceDto getPreferences(Long userId){

        UserPreference preference=  preferenceRepository.findByUserId(userId).orElse(null);
        if(preference == null){
            return new UserPreferenceDto(null, Collections.emptyList(), userId);
        }
        UserPreferenceDto result = new UserPreferenceDto(preference.getPreferredDifficulty(), Collections.emptyList(), userId);
        if(preference.getPreferredTagIds() != null && !preference.getPreferredTagIds().isEmpty()){
            List<TagListItem> tags = tagRepository.findTagsWithCaseCountByIds(preference.getPreferredTagIds()).stream().map(
                    row -> {
                        Long id = row[0] != null ? ((Number) row[0]).longValue() : null;
                        String name = row[1] != null ? row[1].toString() : null;
                        Boolean isActive = row[2] instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(row[2]));
                        Long count = row[3] != null ? ((Number) row[3]).longValue() : null;

                                return new TagListItem(id, name, isActive, count);
                    }
            ).toList();
            result.setPreferredTags(tags);
        }
        return  result;
    }

    @Transactional
    public void updatePreferences(UserPreferenceUpdateRequest request, Long userId) {

        if (request.getPreferredTags() != null && !request.getPreferredTags().isEmpty()) {
            List<Object[]> tags = tagRepository.findTagsWithCaseCountByIds(request.getPreferredTags());

            if (tags.size() != request.getPreferredTags().size()) {
                throw new BadRequestException("One or more tags are invalid or inactive");
            }

            for (Object[] row : tags) {
                boolean isActive = row[2] instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(row[2]));
                if (!isActive) {
                    throw new BadRequestException("One or more tags are invalid or inactive");
                }
            }
        }

        UserPreference preference = preferenceRepository.findByUserId(userId).orElse(null);
        if (preference == null) {
            preference = new UserPreference();
            preference.setUserId(userId);
            preference.setPreferredTagIds(new ArrayList<>());
        }

        if (request.getPreferredDifficulty() != null || Boolean.TRUE.equals(request.getRemoveDifficulty())) {
            preference.setPreferredDifficulty(request.getPreferredDifficulty());
        }

        if (Boolean.TRUE.equals(request.getRemoveTags()) || request.getPreferredTags() != null) {
            if (preference.getPreferredTagIds() == null) {
                preference.setPreferredTagIds(new ArrayList<>());
            }
            preference.getPreferredTagIds().clear();
            if (request.getPreferredTags() != null && !request.getPreferredTags().isEmpty()) {
                preference.getPreferredTagIds().addAll(request.getPreferredTags());
            }
        }

        preferenceRepository.save(preference);
    }

}
