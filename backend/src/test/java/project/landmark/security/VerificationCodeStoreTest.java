package project.landmark.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VerificationCodeStoreTest {

    private final VerificationCodeStore store = new VerificationCodeStore();

    @Test
    void 발급한_번호는_6자리_숫자다() {
        assertThat(store.issue("k")).matches("\\d{6}");
    }

    @Test
    void 맞는_번호는_한_번만_쓸_수_있다() {
        String code = store.issue("k");

        assertThatCode(() -> store.verify("k", code)).doesNotThrowAnyException();
        assertThatThrownBy(() -> store.verify("k", code))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("먼저 요청");
    }

    @Test
    void 요청하지_않은_번호는_거절한다() {
        assertThatThrownBy(() -> store.verify("none", "123456"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 다섯_번_틀리면_맞는_번호도_무효가_된다() {
        String code = store.issue("k");
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> store.verify("k", wrong)).hasMessageContaining("일치하지 않습니다");
        }
        assertThatThrownBy(() -> store.verify("k", wrong)).hasMessageContaining("무효화");
        assertThatThrownBy(() -> store.verify("k", code)).hasMessageContaining("먼저 요청");
    }

    @Test
    void 키가_다르면_섞이지_않는다() {
        String a = store.issue("reset-password:a");
        store.issue("reset-password:b");

        assertThatThrownBy(() -> store.verify("reset-password:b", a.equals("000000") ? "111111" : a))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatCode(() -> store.verify("reset-password:a", a)).doesNotThrowAnyException();
    }
}
