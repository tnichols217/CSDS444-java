{ lib, fetchFromGitHub, jre, jdk, makeWrapper, maven }:
maven.buildMavenPackage rec {
  pname = "PasswordManager";
  version = "0.0.1";

  src = ./.;

  mvnHash = "sha256-3V80IBZvVweIJjNoutor94Bpu4t/j0P6aG1YO7JEC0k=";
  mvnJdk = jdk;

  nativeBuildInputs = [ makeWrapper ];

  installPhase = ''
    mkdir -p $out/bin $out/share/${pname}
    ls **
    mv target/${pname}*.jar target/${pname}.jar
    install -Dm644 target/${pname}.jar $out/share/${pname}

    makeWrapper ${jre}/bin/java $out/bin/${pname} \
      --add-flags "-jar $out/share/${pname}/${pname}.jar"
  '';

  meta.mainProgram = "PasswordManager";
}