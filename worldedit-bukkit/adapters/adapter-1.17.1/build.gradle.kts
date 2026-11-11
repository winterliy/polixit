import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension

applyPaperweightAdapterConfiguration()

dependencies {
    the<PaperweightUserDependenciesExtension>().paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}
