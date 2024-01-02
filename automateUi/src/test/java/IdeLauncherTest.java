import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Test;


public class IdeLauncherTest {
    @Test
    void test() throws InterruptedException {
        final OkHttpClient client=new OkHttpClient();
//        final IdeDownloader ideDownloader=new IdeDownloader(client);
//        Path tmpDir = Path.of("/tmp");
//        Process ideaProcess = IdeLauncher.INSTANCE.launchIde(
//                ideDownloader.downloadAndExtract(Ide.IDEA_COMMUNITY, tmpDir),
//                Map.of("robot-server.port", 8082),
//                List.of(),
//                List.of(ideDownloader.downloadRobotPlugin(tmpDir)),
//                tmpDir
//        );
//        ideaProcess.waitFor();
    }
}
