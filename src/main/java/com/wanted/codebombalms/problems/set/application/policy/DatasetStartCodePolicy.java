package com.wanted.codebombalms.problems.set.application.policy;

import org.springframework.stereotype.Component;

@Component
public class DatasetStartCodePolicy {

    private static final String DATASET_START_CODE = """
            import os
            import pandas as pd

            df = pd.read_csv(os.environ["DATASET_PATH"])
            """;

    public String create() {
        return DATASET_START_CODE;
    }

    public String createIfDatasetExists(boolean datasetExists) {
        return datasetExists ? DATASET_START_CODE : null;
    }
}
