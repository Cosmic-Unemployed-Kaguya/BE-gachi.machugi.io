package kaguya.domain.quiz.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kaguya.domain.quiz.model.dto.request.QuizCreateReq;
import kaguya.domain.quiz.model.dto.request.QuizUpdateReq;
import kaguya.domain.quiz.model.dto.response.QuizRes;
import kaguya.domain.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    // 퀴즈 생성 API
    @PostMapping
    public ResponseEntity<QuizRes> createQuiz(@RequestBody QuizCreateReq request) {
        QuizRes response = quizService.createQuiz(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 퀴즈 조회 API
    @GetMapping("/{quizIdx}")
    public ResponseEntity<QuizRes> getQuiz(@PathVariable("quizIdx") Long quizIdx) {
        QuizRes response = quizService.getQuiz(quizIdx);
        return ResponseEntity.ok(response);
    }

    // 퀴즈 전체 조회 API
    @GetMapping
    public ResponseEntity<List<QuizRes>> getQuizList() {
        List<QuizRes> response = quizService.getQuizList();
        return ResponseEntity.ok(response);
    }

    // 퀴즈 수정 API
    @PatchMapping("/{quizIdx}")
    public ResponseEntity<QuizRes> updateQuiz(
            @PathVariable("quizIdx") Long quizIdx,
            @RequestBody QuizUpdateReq request
    ) {
        QuizRes response = quizService.updateQuiz(quizIdx, request);
        return ResponseEntity.ok(response);
    }

    // 퀴즈 삭제 API
    @DeleteMapping("/{quizIdx}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("quizIdx") Long quizIdx) {
        quizService.deleteQuiz(quizIdx);
        return ResponseEntity.noContent().build();
    }
}