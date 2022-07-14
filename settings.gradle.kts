rootProject.name = "cfg-based-inference"

includeBuild ("../annotation-tools/annotation-file-utilities") {
    if (!file("../annotation-tools/annotation-file-utilities").exists()) {
        exec {
            executable("scripts/.ci-build-without-test.sh")
        }
    }
}
