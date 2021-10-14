plugins {
    id("com.example.report-aggregation")
}

dependencies {
    aggregate("com.example.myproduct.user-feature:table")
    aggregate("com.example.myproduct.admin-feature:config")
}
