-injars       dist/jterm.jar(
META-INF/MANIFEST.MF,
!META-INF/**
)
-outjars      dist/jterm-shrinked.jar
-libraryjars  <java.home>/lib/rt.jar
-printmapping out.map
-dontobfuscate
-printusage

-assumenosideeffects class de.topobyte.jterm.core.TerminalWidget {
    private void log(...);
}

-keepclassmembernames class de.topobyte.jterm.core.Terminal {
    int mfd;
}

-keep public class de.topobyte.jterm.JTerm {
	public static void main(java.lang.String[]);
}
