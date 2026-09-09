package com.project.main.service.cases;

import com.project.main.exception.BadRequestException;
import com.project.main.repository.cases.CaseCompletionRepository;
import com.project.main.repository.cases.CaseRatingRepository;
import com.project.main.repository.cases.CaseRepository;
import com.project.main.repository.cases.CaseTagRepository;
import com.project.main.repository.cases.TagRepository;
import com.project.main.repository.learning.StudyMaterialRepository;
import com.project.main.service.common.S3StorageService;
import com.project.main.service.component.TypeMapperComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class CaseServiceValidationTest {

    @Mock private CaseRepository caseRepository;
    @Mock private TagRepository tagRepository;
    @Mock private CaseTagRepository caseTagRepository;
    @Mock private S3StorageService s3StorageService;
    @Mock private CaseCompletionRepository completionRepository;
    @Mock private CaseRatingRepository caseRatingRepository;
    @Mock private StudyMaterialRepository materialRepository;

    private CaseService caseService;

    @BeforeEach
    void setUp() {
        caseService = new CaseService(
                caseRepository, tagRepository, caseTagRepository, s3StorageService,
                completionRepository, caseRatingRepository, materialRepository,
                new TypeMapperComponent());
    }

    @Test
    void getPublicCasesRejectsNegativePage() {
        assertThatThrownBy(() -> caseService.getPublicCases(-1, 25, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Page cannot be negative");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 101}) // границы: size допустим только 1..100
    void getPublicCasesRejectsInvalidSize(int size) {
        assertThatThrownBy(() -> caseService.getPublicCases(0, size, null, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Size must be between 1 and 100");
    }

    @Test
    void getPublicCasesRejectsTooLongSearch() {
        String longSearch = "a".repeat(201);

        assertThatThrownBy(() -> caseService.getPublicCases(0, 25, longSearch, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Search query is too long");
    }
}