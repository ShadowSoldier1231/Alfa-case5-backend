package com.project.main.service.learning;

import com.project.main.dto.learing.*;
import com.project.main.exception.BadRequestException;
import com.project.main.exception.ConflictException;
import com.project.main.exception.InternalServerErrorException;
import com.project.main.exception.NotFoundException;
import com.project.main.model.learning.*;
import com.project.main.repository.learning.*;
import com.project.main.service.component.TypeMapperComponent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final StudyMaterialRepository materialRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final TypeMapperComponent typeMapper;

    public QuizService(StudyMaterialRepository materialRepository,
                       QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       AnswerOptionRepository answerOptionRepository,
                       QuizAttemptRepository attemptRepository,
                       UserAnswerRepository userAnswerRepository,
                       TypeMapperComponent typeMapper) {
        this.materialRepository = materialRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerOptionRepository = answerOptionRepository;
        this.attemptRepository = attemptRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.typeMapper = typeMapper;
    }

    @Transactional(readOnly = true)
    public TheoryQuizResponse getQuizByMaterialId(Long materialId) {
        if (materialId == null || materialId <= 0) {
            throw new BadRequestException("Invalid material ID");
        }

        Optional<Quiz> quizOptional = quizRepository.findActiveQuizByMaterialId(materialId);

        if (quizOptional.isEmpty()) {
            boolean materialExists = materialRepository
                    .findActiveByIdAndActiveCase(materialId)
                    .isPresent();

            if (!materialExists) {
                throw new NotFoundException("Material not found");
            }

            throw new NotFoundException("Quiz not found");
        }

        Quiz quiz = quizOptional.get();

        List<Question> questions = questionRepository.findActiveByQuizId(quiz.getId());

        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        Map<Long, List<TheoryQuizResponse.OptionDto>> optionsByQuestionId;

        if (questionIds.isEmpty()) {
            optionsByQuestionId = Map.of();
        } else {
            optionsByQuestionId = answerOptionRepository.findByQuestionIdIn(questionIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            AnswerOption::getQuestionId,
                            LinkedHashMap::new,
                            Collectors.mapping(
                                    option -> new TheoryQuizResponse.OptionDto(
                                            option.getId(),
                                            option.getPosition(),
                                            option.getText()
                                    ),
                                    Collectors.toList()
                            )
                    ));
        }

        List<TheoryQuizResponse.QuestionDto> questionDtos = questions.stream()
                .map(question -> new TheoryQuizResponse.QuestionDto(
                        question.getId(),
                        question.getPosition(),
                        question.getText(),
                        optionsByQuestionId.getOrDefault(question.getId(), List.of())
                ))
                .toList();

        return new TheoryQuizResponse(quiz.getId(), questionDtos);
    }

    @Transactional(readOnly = true)
    public AdminTheoryQuizResponse getAdminQuizByMaterialId(Long materialId) {
        if (materialId == null || materialId <= 0) {
            throw new BadRequestException("Invalid material ID");
        }

        boolean materialExists = !materialRepository
                .findAdminMaterialById(materialId)
                .isEmpty();

        if (!materialExists) {
            throw new NotFoundException("Material not found");
        }

        List<Quiz> quizzes = quizRepository.findAllByMaterialId(materialId);

        if (quizzes.isEmpty()) {
            throw new NotFoundException("Quiz not found");
        }

        if (quizzes.size() > 1) {
            throw new InternalServerErrorException("Multiple quizzes attached to this material");
        }

        Quiz quiz = quizzes.get(0);

        List<Question> questions = questionRepository.findAllByQuizId(quiz.getId());

        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();

        Map<Long, List<AdminTheoryQuizResponse.AdminOptionDto>> optionsByQuestionId;

        if (questionIds.isEmpty()) {
            optionsByQuestionId = Map.of();
        } else {
            optionsByQuestionId = answerOptionRepository.findByQuestionIdIn(questionIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            AnswerOption::getQuestionId,
                            LinkedHashMap::new,
                            Collectors.mapping(
                                    option -> new AdminTheoryQuizResponse.AdminOptionDto(
                                            option.getId(),
                                            option.getQuestionId(),
                                            option.getText(),
                                            option.getPosition(),
                                            option.getCorrect()
                                    ),
                                    Collectors.toList()
                            )
                    ));
        }

        List<AdminTheoryQuizResponse.AdminQuestionDto> questionDtos = questions.stream()
                .map(question -> new AdminTheoryQuizResponse.AdminQuestionDto(
                        question.getId(),
                        question.getQuizId(),
                        question.getText(),
                        question.getPosition(),
                        question.getActive(),
                        optionsByQuestionId.getOrDefault(question.getId(), List.of())
                ))
                .toList();

        return new AdminTheoryQuizResponse(
                quiz.getId(),
                quiz.getMaterialId(),
                quiz.getTitle(),
                quiz.getActive(),
                questionDtos
        );
    }


    @Transactional
    public QuizAttemptResponse submitQuizAttempt(Long userId, Long quizId, QuizSubmitRequest request) {
        if (quizId == null || quizId <= 0) {
            throw new BadRequestException("Invalid quiz ID");
        }

        Quiz quiz = quizRepository.findActiveQuizWithActiveMaterialById(quizId)
                .orElseThrow(() -> new NotFoundException("Quiz not found"));

        if (request == null || request.getAnswers() == null || request.getAnswers().isEmpty()) {
            throw new BadRequestException("Answers cannot be empty");
        }

        Set<Long> uniqueQuestionIds = new HashSet<>();
        List<Long> answerOptionIds = new ArrayList<>();

        for (QuizSubmitRequest.UserAnswerDto ans : request.getAnswers()) {
            if (ans.getQuestionId() == null || ans.getAnswerOptionId() == null) {
                throw new BadRequestException("Invalid answer format");
            }
            if (!uniqueQuestionIds.add(ans.getQuestionId())) {
                throw new BadRequestException("Duplicate question ID in request");
            }
            answerOptionIds.add(ans.getAnswerOptionId());
        }

        List<Long> activeQuestionIds = questionRepository.findActiveQuestionIdsByQuizId(quizId);
        if (activeQuestionIds.isEmpty()) {
            throw new BadRequestException("Quiz has no active questions");
        }

        if (!new HashSet<>(activeQuestionIds).containsAll(uniqueQuestionIds)) {
            throw new BadRequestException("One or more questions do not belong to this quiz or are inactive");
        }

        List<Object[]> optionRows = answerOptionRepository.findValidationDataByIds(answerOptionIds);

        Map<Long, OptionValidationData> optionsById = optionRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> new OptionValidationData(
                                ((Number) row[1]).longValue(),
                                typeMapper.toBoolean(row[2])
                        ),
                        (existing, replacement) -> existing
                ));

        if (optionsById.size() != new HashSet<>(answerOptionIds).size()) {
            throw new BadRequestException("One or more answer options do not exist");
        }

        for (QuizSubmitRequest.UserAnswerDto ans : request.getAnswers()) {
            OptionValidationData option = optionsById.get(ans.getAnswerOptionId());

            if (option == null || !option.questionId().equals(ans.getQuestionId())) {
                throw new BadRequestException("One or more answer options do not belong to the question");
            }
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuizId(quizId);
        attempt.setUserId(userId);
        attempt.setTotalQuestions(activeQuestionIds.size());

        QuizAttempt savedAttempt = attemptRepository.saveAndFlush(attempt);

        int correctAnswers = 0;
        List<UserAnswer> userAnswers = new ArrayList<>();

        for (QuizSubmitRequest.UserAnswerDto ans : request.getAnswers()) {
            boolean isCorrect = Boolean.TRUE.equals(
                    optionsById.get(ans.getAnswerOptionId()).correct()
            );
            if (isCorrect) {
                correctAnswers++;
            }

            UserAnswer ua = new UserAnswer();
            ua.setUserId(userId);
            ua.setQuestionId(ans.getQuestionId());
            ua.setAnswerOptionId(ans.getAnswerOptionId());
            ua.setAttemptId(savedAttempt.getId());
            userAnswers.add(ua);
        }

        int totalQuestions = activeQuestionIds.size();
        int score = (correctAnswers * 100) / totalQuestions;
        boolean isSolved = score >= 70;

        savedAttempt.setCorrectAnswers(correctAnswers);
        savedAttempt.setScore(score);
        savedAttempt.setIsSolved(isSolved);

        attemptRepository.save(savedAttempt);
        userAnswerRepository.saveAll(userAnswers);

        return new QuizAttemptResponse(
                savedAttempt.getId(),
                correctAnswers,
                totalQuestions,
                score,
                isSolved
        );
    }


    @Transactional(readOnly = true)
    public QuizStatusResponse getQuizStatus(Long userId, Long quizId) {
        if (quizId == null || quizId <= 0) {
            throw new BadRequestException("Invalid quiz ID");
        }

        if (quizRepository.findActiveQuizWithActiveMaterialById(quizId).isEmpty()) {
            throw new NotFoundException("Quiz not found");
        }

        Object[] result = attemptRepository.getQuizStatusByUserAndQuiz(userId, quizId);


        Integer attemptsCount = 0;
        Boolean isSolved = false;
        Integer score = 0;

        if(result != null && result.length >=3){
            attemptsCount =  result[0] != null ? ((Number) result[0]).intValue() : 0;
            isSolved = typeMapper.toBoolean(result[1]);
            score = result[2] != null ? ((Number) result[2]).intValue() : 0;
        }

        return new QuizStatusResponse(quizId, attemptsCount, isSolved, score);
    }

    @Transactional
    public Long upsertQuiz(Long materialId, QuizUpsertRequest request) {

        if (materialId == null || materialId <= 0) {
            throw new BadRequestException("Invalid material ID");
        }

        if (!materialRepository.existsById(materialId)) {
            throw new NotFoundException("Material not found");
        }

        Set<Integer> questionPositions = new HashSet<>();
        for (QuestionUpsertDto q : request.getQuestions()) {
            if (!questionPositions.add(q.getPosition())) {
                throw new BadRequestException("Duplicate question position: " + q.getPosition());
            }

            Set<Integer> optionPositions = new HashSet<>();
            boolean hasCorrect = false;
            for (OptionUpsertDto o : q.getOptions()) {
                if (!optionPositions.add(o.getPosition())) {
                    throw new BadRequestException("Duplicate option position: " + o.getPosition() + " in question " + q.getPosition());
                }
                if (Boolean.TRUE.equals(o.getIsCorrect())) {
                    hasCorrect = true;
                }
            }
            if (!hasCorrect) {
                throw new BadRequestException("At least one correct option is required for question " + q.getPosition());
            }
        }

        List<Quiz> existingQuizzes = quizRepository.findAllByMaterialId(materialId);
        if (existingQuizzes.size() > 1) {
            throw new InternalServerErrorException("Multiple quizzes attached to this material");
        }

        Quiz quiz;
        if (!existingQuizzes.isEmpty()) {
            quiz = existingQuizzes.get(0);

            if (attemptRepository.countByQuizId(quiz.getId()) > 0) {
                throw new ConflictException("Quiz has attempts and cannot be updated");
            }

            quiz.setTitle(request.getTitle());
            quiz.setActive(request.getIsActive());
            quizRepository.save(quiz);

            List<Question> existingQuestions = questionRepository.findAllByQuizId(quiz.getId());
            for (Question eq : existingQuestions) {
                answerOptionRepository.deleteByQuestionId(eq.getId());
            }
            questionRepository.deleteByQuizId(quiz.getId());
        } else {
            quiz = new Quiz();
            quiz.setMaterialId(materialId);
            quiz.setTitle(request.getTitle());
            quiz.setActive(request.getIsActive());
            quiz = quizRepository.save(quiz);
        }

        for (QuestionUpsertDto qDto : request.getQuestions()) {
            Question q = new Question();
            q.setQuizId(quiz.getId());
            q.setText(qDto.getText());
            q.setPosition(qDto.getPosition());
            q.setActive(qDto.getIsActive() != null ? qDto.getIsActive() : true);
            q = questionRepository.save(q);

            for (OptionUpsertDto oDto : qDto.getOptions()) {
                AnswerOption o = new AnswerOption();
                o.setQuestionId(q.getId());
                o.setText(oDto.getText());
                o.setPosition(oDto.getPosition());
                o.setCorrect(oDto.getIsCorrect());
                answerOptionRepository.save(o);
            }
        }
        return quiz.getId();
    }


    private record OptionValidationData(Long questionId, Boolean correct) {}
}