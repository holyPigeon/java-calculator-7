package calculator.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import calculator.validation.InputValidator;

public class InputParser {
	public InputParser(String input) {
		// extractCustomDelimiter(input).ifPresent(delimiter::add);
	}

	/**
	 * 핵심 로직
	 */
	// 1차 가공된 입력값에서 숫자를 추출
	public List<Integer> extractNumbers(String input, Delimiter delimiter) {
		if (input.isBlank()) {
			return List.of();
		}
		String[] splitParts = splitInput(input, delimiter);

		return saveNumbers(splitParts);
	}

	// 구분자들을 이용해 정규식을 만들고, 이를 이용해 입력값을 분리
	private String[] splitInput(String input, Delimiter delimiter) {
		String processedInput = removeCustomDelimiter(input);
		InputValidator.validateInvalidDelimiter(processedInput, delimiter);

		String regex = String.join("|",
			delimiter.getDelimiters().stream()
				.map(Pattern::quote) // 구분자를 정규식에 안전하게 포함 (특수 문자의 경우 혼동의 여지가 있음)
				.toArray(String[]::new)
		);

		return processedInput.split(regex);
	}

	// 입력값에서 커스텀 구분자를 제거한 문자열을 반환
	private String removeCustomDelimiter(String input) {
		if (hasCustomDelimiter(input)) {
			int delimiterEnd = input.indexOf("\\n");

			return input.substring(delimiterEnd + 2); // \n 이후의 문자열 반환
		}

		return input; // 커스텀 구분자가 없을 경우 원본 문자열 반환
	}

	// 입력값에 커스텀 구분자가 있는지 확인
	private boolean hasCustomDelimiter(String input) {
		return input.startsWith("//");
	}

	// 분리된 각 부분에서 숫자로 변환하여 리스트에 추가
	private List<Integer> saveNumbers(String[] splitParts) {
		List<Integer> numbers = new ArrayList<>();
		Arrays.stream(splitParts)
			.map(String::trim)
			.forEach(part -> {
				InputValidator.validateDigit(part);
				InputValidator.validateNumberPositive(part);
				numbers.add(Integer.parseInt(part));
			});

		return numbers;
	}

	/**
	 * 문자열 전처리
	 */
	// 입력값에서 커스텀 구분자를 추출 (없을 수 있음)
	private Optional<String> extractCustomDelimiter(String input) {
		if (!hasCustomDelimiter(input)) {
			return Optional.empty();
		}
		String customDelimiter = parseCustomDelimiter(input);

		return Optional.of(customDelimiter);
	}

	private String parseCustomDelimiter(String input) {
		int delimiterStart = input.indexOf("//") + 2;
		int delimiterEnd = input.indexOf("\\n");
		if (delimiterEnd == -1) {
			throw new IllegalArgumentException("커스텀 구분자의 끝을 의미하는 \\n이 없습니다.");
		}
		return input.substring(delimiterStart, delimiterEnd);
	}
}
