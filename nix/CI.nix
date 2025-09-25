{ stdenv, gnutar, jar, appimage, ... }:
stdenv.mkDerivation rec {
  pname = "PasswordManager";
  version = "0.0.1";
  src = ./../PasswordManager;
  installPhase = ''
    mkdir -p $out
    cp ${jar.out}/share/${pname}/*.jar $out
    cp ${appimage.out} $out/${pname}.AppImage
    ${gnutar}/bin/tar -czf $out/source.tgz .
  '';
}