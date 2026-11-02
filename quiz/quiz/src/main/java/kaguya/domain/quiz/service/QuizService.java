package kaguya.domain.quiz.service;

import java.util.List;

import kaguya.domain.quiz.model.dto.request.QuizCreateReq;
import kaguya.domain.quiz.model.dto.request.QuizUpdateReq;
import kaguya.domain.quiz.model.dto.response.QuizRes;

public interface QuizService {

    // 퀴즈 생성 
    QuizRes createQuiz(QuizCreateReq request);

    // 퀴즈 조회
    QuizRes getQuiz(Long quizIdx);

    // 퀴즈 전체 조회
    List<QuizRes> getQuizList();

    // 퀴즈 수정
    QuizRes updateQuiz(Long quizIdx, QuizUpdateReq request);

    // 퀴즈 삭제
    void deleteQuiz(Long quizIdx);
}