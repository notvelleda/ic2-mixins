package xyz.velleda.ic2mixins;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.apache.commons.io.FileUtils;

public class LoadJarPlugin implements IFMLLoadingPlugin {
    public LoadJarPlugin() {
        System.out.println("hello, coremod world!");

        this.findAndLoadClass("ic2/core/block/machine/tileentity/TileEntityRecycler$RecyclerRecipeManager.class");
        this.findAndLoadClass("ic2/core/block/machine/tileentity/TileEntityStandardMachine.class");
        this.findAndLoadClass("ic2/core/block/machine/tileentity/TileEntityMatter.class");

        System.out.println("loaded classes!");
    }

    // https://gist.github.com/happyzleaf/1b98c6c5e7e30c2861e0a381c347460b
    public static void findAndLoadClass(String className) {
        try {
            Class.forName(className, false, LoadJarPlugin.class.getClassLoader());

            System.out.println("already loaded JAR for class " + className);
            return;
        } catch (Exception ignored) {}

        File mod;

        File modsFolder = new File(System.getProperty("user.dir"), "mods");
        if (!modsFolder.exists()) {
            throw new RuntimeException("the mods folder at " + modsFolder.toString() + " doesn't exist!");
        }

        mod = FileUtils.listFiles(modsFolder, new String[]{"jar"}, false).stream()
            .filter(jarFile -> containsClass(className, jarFile))
            .findAny().orElse(null);

        if (mod == null) {
            throw new RuntimeException("couldn't find JAR containing class " + className);
        }

        try {
            if (!CoreModManager.getReparseableCoremods().contains(mod.getName())) {
                Path backupPath = Paths.get(mod.getCanonicalPath() + ".original");

                if (backupPath.toFile().exists()) {
                    System.out.println(backupPath.toString() + " exists, assuming " + mod.getCanonicalPath() + " has already been stripped of META-INF");
                } else {
                    System.out.println("backing up " + mod.getCanonicalPath() + " to " + backupPath.toString());
                    Files.copy(mod.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);

                    // remove the signing info so private class mixins will work properly
                    System.out.println("stripping META-INF from " + mod.toURI().toURL().toString());
                    try (FileSystem fs = FileSystems.newFileSystem(mod.toPath(), null)) {
                        // https://stackoverflow.com/a/27917071
                        Files.walkFileTree(fs.getPath("/META-INF"), new SimpleFileVisitor<Path>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                    }
                }

                System.out.println("loading JAR " + mod.getCanonicalPath() + " for class " + className);
                ((LaunchClassLoader) LoadJarPlugin.class.getClassLoader()).addURL(mod.toURI().toURL());
                CoreModManager.getReparseableCoremods().add(mod.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("couldn't load class " + className + ": " + e.toString());
        }
    }

    private static boolean containsClass(String className, File jarFile) {
        try (ZipInputStream zip = new ZipInputStream(new FileInputStream(jarFile))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                zip.closeEntry();

                if (entry.getName().equals(className)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        return false;
    }

    public String getAccessTransformerClass() {
        return null;
    }

    public String[] getASMTransformerClass() {
        return null;
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {}
}
