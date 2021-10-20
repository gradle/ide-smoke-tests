package com.example.myproduct.user.table;

import com.example.myproduct.admin.state.ConfigurationState;
import com.example.myproduct.model.MyProductRelease;
import com.example.myproduct.user.data.DataRetriever;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableBuilder {

    // TODO (scenario) Resources from test configuration are not available in the main source set
    //   Instructions:
    //   - Uncomment the code snippet below and verify that
    //     - it gives compilation error
    //     - it offers a quick fix to add a dependency on the test module

    // private static com.example.myproduct.user.table.TableBuilderIntegrationTest();

    public static List<List<String>> build() {
        List<MyProductRelease> releases = DataRetriever.retrieve().getReleases();
        return build(releases);
    }

    protected static List<List<String>> build(List<MyProductRelease> releases) {
        return releases.stream().filter(TableBuilder::isInRange).map(r ->
                Arrays.asList(r.getVersion(), r.getReleaseNotes())).collect(Collectors.toList());
    }

    protected static boolean isInRange(MyProductRelease release) {
        String current = release.getVersion();
        String min = ConfigurationState.INSTANCE.getRangeSetting().getMinVersion();
        String max = ConfigurationState.INSTANCE.getRangeSetting().getMaxVersion();
        return (min.compareTo(current) <= 0 || min.isEmpty()) && (max.compareTo(current) >= 0 || max.isEmpty());
    }

}
