package com.gstncaruso.tabpro.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class DebianAppearanceDependenciesTest {

    @Test
    void givenDebianPackage_whenReadingDependencies_thenSystemAppearanceRequirementsAreMandatory() throws IOException {
        Path control = Path.of(System.getProperty("user.dir"), "..", "tabpro-app", "src", "deb", "control", "control")
                .normalize();
        String metadata = Files.readString(control);
        String depends = Pattern.compile("(?m)^Depends:([^\\r\\n]*(?:\\r?\\n[ \\t]+[^\\r\\n]*)*)")
                .matcher(metadata)
                .results()
                .map(match -> match.group(1))
                .findFirst()
                .orElse("");
        List<String> dependencyGroups = Arrays.stream(depends.split(",")).map(String::strip).toList();
        String optionalVersion = "(?:\\s*\\([^)]*\\))?";
        String portalBackend = "xdg-desktop-portal-(?:gtk|gnome|kde|backend)" + optionalVersion;
        Pattern portalBackendGroup = Pattern.compile(portalBackend + "(?:\\s*\\|\\s*" + portalBackend + ")*");
        List<String> missing = List.of("libglib2.0-bin", "xdg-desktop-portal", "xdg-desktop-portal-backend").stream()
                .filter(required -> dependencyGroups.stream()
                        .noneMatch(group -> required.equals("xdg-desktop-portal-backend")
                                ? portalBackendGroup.matcher(group).matches()
                                : group.matches(Pattern.quote(required) + optionalVersion)))
                .toList();

        assertEquals(List.of(), missing,
                "Debian Depends must require gdbus, the settings portal and a backend group with only valid portal providers; "
                        + "Recommends does not guarantee system appearance detection on a fresh install. Depends:" + depends);
    }
}
