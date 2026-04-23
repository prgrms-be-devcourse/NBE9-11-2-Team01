# Backend — NBE9-11-2 Team01

Spring Boot API 모듈입니다. 공통은 **`global.entity`**, **`global.error`**, **`global.response`** 세 가지만 둡니다.

---

## 요구 사항

- JDK 25 (Gradle toolchain)

---

## 실행

```bash
cd backend
./gradlew.bat compileJava
./gradlew.bat test
./gradlew.bat bootRun
```

---

## Global 구조 (3가지만)

| 패키지 | 역할 |
|--------|------|
| `global.entity` | `BaseEntity` — `id`, `createdAt`, `modifiedAt` |
| `global.error` | `GlobalExceptionHandler` — 검증·404·기타 예외 → `ApiResponse` 실패 본문 |
| `global.response` | `ApiResponse` — 성공/실패 공통 JSON (`success`, `code`, `message`, `data`) |

```
src/main/java/com/team01/backend/global/
  entity/       BaseEntity.java
  error/        GlobalExceptionHandler.java
  response/     ApiResponse.java
```

---

## 작성 예시

### 엔티티

```java
import com.team01.backend.global.entity.BaseEntity;
// @Entity …
public class Post extends BaseEntity { /* 필드 */ }
```

### 컨트롤러 — 성공 시 `ApiResponse`로 감싸서 반환

```java
import com.team01.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ofSuccess(postService.get(id)));
    }
}
```

### 서비스 — 없을 때 예외

```java
return postRepository.findById(id)
        .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException());
// 또는 규칙 위반 시 한글 메시지와 함께:
throw new IllegalArgumentException("본인만 수정할 수 있습니다.");
```

---

## 응답 JSON 예시

**성공**

```json
{ "success": true, "data": { "id": 1, "title": "제목" } }
```

**실패 (검증)**

```json
{
  "success": false,
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다. (title: …)"
}
```

**실패 (404)**

```json
{
  "success": false,
  "code": "NOT_FOUND",
  "message": "요청하신 데이터를 찾을 수 없습니다."
}
```

`@JsonInclude(NON_NULL)` 이라 성공 시 `code`·`message`는 보통 생략됩니다.

---

## PR 시 참고

- 성공 응답은 **`ApiResponse.ofSuccess(…)`** 로 맞추면 프론트가 타입 하나로 처리하기 좋습니다.
- 비즈니스 오류는 **`IllegalArgumentException` + 한글 메시지** 또는 **`EntityNotFoundException`** 정도로 단순히 올려도 됩니다.
