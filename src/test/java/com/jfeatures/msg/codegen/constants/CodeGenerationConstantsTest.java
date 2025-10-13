package com.jfeatures.msg.codegen.constants;

import org.junit.jupiter.api.Test;

class CodeGenerationConstantsTest {

    @Test
    void testBuilderPatternThresholdUsage() {
        // Test that the threshold value makes sense for builder pattern usage
        int threshold = CodeGenerationConstants.BUILDER_PATTERN_FIELD_THRESHOLD;

        // Should trigger builder for large field counts
        assert threshold >= 3 : "Threshold should be at least 3 for meaningful builder pattern usage";

        // Should not be too high to be practical
        assert threshold <= 1000 : "Threshold should not be impractically high";
    }
}
