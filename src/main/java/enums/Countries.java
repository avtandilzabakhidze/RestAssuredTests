package enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Countries {
    DUTCH("Dutch"),
    BRAZILIAN("Brazilian"),
    CANADIAN("Canadian");

    private final String value;
}
