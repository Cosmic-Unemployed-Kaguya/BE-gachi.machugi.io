package kaguya.domain.quiz.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kaguya.domain.quiz.model.dto.request.QuizCreateReq;
import kaguya.domain.quiz.model.dto.request.QuizUpdateReq;
import kaguya.domain.quiz.model.dto.response.QuizRes;
import kaguya.domain.quiz.model.entity.QuizEntity;
import kaguya.domain.quiz.repository.QuizRepository;
import kaguya.domain.quiz.service.QuizService;
import kaguya.domain.quiz.util.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizServiceImpl implements QuizService {

    private final QuizRepository quizRepository;
    private final QuizMapper quizMapper;

    // 퀴즈 생성
    @Override
    public QuizRes createQuiz(QuizCreateReq request) {

        QuizEntity quiz = quizMapper.requestToEntity(request);
        QuizEntity savedQuiz = quizRepository.save(quiz);

        return quizMapper.entityToResponse(savedQuiz);
    }

    // 퀴즈 조회
    @Transactional(readOnly = true)
    @Override
    public QuizRes getQuiz(Long quizIdx) {

        QuizEntity quiz = quizRepository.findById(quizIdx)
                .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

        return quizMapper.entityToResponse(quiz);
    }

    // 퀴즈 전체 조회
    @Transactional(readOnly = true)
    @Override
    public List<QuizRes> getQuizList() {

        List<QuizEntity> quizzes = quizRepository.findAll();

        return quizzes.stream()
                .map(quizMapper::entityToResponse)
                .toList();
    }

    // 퀴즈 수정
    @Override
    public QuizRes updateQuiz(Long quizIdx, QuizUpdateReq request) {

        QuizEntity quiz = quizRepository.findById(quizIdx)
                .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

        quiz.updateQuiz(
                request.title(),
                request.description(),
                request.thumbnail(),
                request.category()
        );

        return quizMapper.entityToResponse(quiz);
    }

    // 퀴즈 삭제
    @Override
    public void deleteQuiz(Long quizIdx) {
        QuizEntity quiz = quizRepository.findById(quizIdx)
                .orElseThrow(() -> new RuntimeException("퀴즈가 존재하지 않습니다."));

        quizRepository.delete(quiz);
    }
}