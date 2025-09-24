{ lib, fetchFromGitHub, jre, jdk, makeWrapper, maven }:
maven.buildMavenPackage rec {
  pname = "PasswordManager";
  version = "0.0.1";

  src = ./.;

  mvnHash = "sha256-AiMj3En9qzzaJVbxEoE4GAnm941HrXmK12YlU1fyNZw=";
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
}