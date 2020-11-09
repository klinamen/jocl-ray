package io.klinamen.joclray.tonemapping;

import io.klinamen.joclray.util.FloatVec4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeToneMapping implements ToneMappingOperator {
    private final List<ToneMappingOperator> operators;

    public CompositeToneMapping(ToneMappingOperator... operators) {
        this.operators = Arrays.asList(operators);
    }

    public CompositeToneMapping() {
        this.operators = new ArrayList<>();
    }

    public CompositeToneMapping add(ToneMappingOperator operator) {
        this.operators.add(operator);
        return this;
    }

    @Override
    public FloatVec4 toneMap(FloatVec4 radiance) {
        return operators.stream()
                .reduce((op, op2) -> (r -> op2.toneMap(op.toneMap(r))))
                .map(op -> op.toneMap(radiance))
                .orElse(radiance);
    }
}
