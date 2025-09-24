{
  inputs = {
    flake-utils.url = "github:numtide/flake-utils";
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";
    treefmt-nix.url = "github:numtide/treefmt-nix";
    appimage.url = "github:ralismark/nix-appimage";
  };
  outputs = {...} @ inputs:
    inputs.flake-utils.lib.eachDefaultSystem (
      system: let
        pkgs = (import inputs.nixpkgs) {
          inherit system;
          config = {
            allowUnfree = true;
          };
        };
        mv = pkgs.callPackage inputs.mavenix {};
        jdk = pkgs.openjdk.override {
          enableJavaFX = true;
          openjfx_jdk = pkgs.openjfx.override { withWebKit = true; };
        };
      in {
        devShells = rec {
          d = pkgs.mkShell {
            packages = [ jdk ] ++ (with pkgs; [
              podman-compose
              podman
              openssl
              maven
              
            ]);
            MAVEN_OPTS = "-Dmaven.repo.local=${(pkgs.callPackage ./PasswordManager { inherit jdk; jre = jdk; }).fetchedMavenDeps}";
          };
          default = d;
        };
        packages = rec {
          jar = pkgs.callPackage ./PasswordManager { inherit jdk; jre = jdk; };
          appimage = inputs.appimage.lib.${system}.mkAppImage { program = pkgs.lib.getExe jar; };
          CI = pkgs.callPackage ./nix/CI.nix { inherit jar appimage; };
          default = jar;
        };
        formatter = let
          treefmtconfig = inputs.treefmt-nix.lib.evalModule pkgs {
            projectRootFile = "flake.nix";
            programs = {
              alejandra.enable = true;
              black.enable = true;
              toml-sort.enable = true;
              yamlfmt.enable = true;
              mdformat.enable = true;
              shellcheck.enable = true;
              shfmt.enable = true;
            };
            settings.formatter = {
              shellcheck.excludes = [".envrc"];
            };
          };
        in
          treefmtconfig.config.build.wrapper;
      }
    );
}
