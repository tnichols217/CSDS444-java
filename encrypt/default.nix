{ lib, fetchFromGitHub, jre, jdk, makeWrapper, maven }:
maven.buildMavenPackage rec {
  pname = "encrypt";
  version = "0.0.1";

  src = ./.;

  mvnHash = "sha256-jX6JX1O+W0+k7iEXDl+J0k7TrrS7XGAhqbslBeurbto=";
  mvnJdk = jdk;

  nativeBuildInputs = [ makeWrapper ];

  installPhase = ''
    mkdir -p $out/bin $out/share/${pname}
    mv target/${pname}*.jar target/${pname}.jar
    ls **
    install -Dm644 target/${pname}.jar $out/share/${pname}

    makeWrapper ${jre}/bin/java $out/bin/${pname} \
      --add-flags "-jar $out/share/${pname}/${pname}.jar"
  '';
}