<h1 align="center">Copy as Python aiohttp</h1>

<p align="center">
  <img alt="Github top language" src="https://img.shields.io/github/languages/top/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8">

  <img alt="Github language count" src="https://img.shields.io/github/languages/count/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8">

  <img alt="Repository size" src="https://img.shields.io/github/repo-size/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8">

  <img alt="License" src="https://img.shields.io/github/license/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8">

  <!-- <img alt="Github issues" src="https://img.shields.io/github/issues/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8" /> -->

  <!-- <img alt="Github forks" src="https://img.shields.io/github/forks/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8" /> -->

  <!-- <img alt="Github stars" src="https://img.shields.io/github/stars/y0k4i-1337/copy-as-python-aiohttp?color=56BEB8" /> -->
</p>

<!-- Status -->

<p align="center">
  <a href="#snake-about">About</a> &#xa0; | &#xa0;
  <a href="#sparkles-features">Features</a> &#xa0; | &#xa0;
  <a href="#rocket-technologies">Technologies</a> &#xa0; | &#xa0;
  <a href="#hammer-building">Building</a> &#xa0; | &#xa0;
  <a href="#checkered_flag-starting">Starting</a> &#xa0; | &#xa0;
  <a href="#memo-license">License</a> &#xa0; | &#xa0;
  <a href="https://github.com/y0k4i-1337" target="_blank">Author</a>
</p>

<br>

## :snake: About ##

A Burp extension to generate async Python code from HTTP requests.

This extension generates different flavors of scripts (*e.g.* with/without
session, with/without main function).

The resulting codes have been tested with `Python 3.11.2` but should work with
other versions as well.

## :sparkles: Features ##

:heavy_check_mark: Generate individual async functions from requests;\
:heavy_check_mark: Combine multiple requests into a single session;\
:heavy_check_mark: Generate async script for password spraying attacks.

## :rocket: Technologies ##

The following tools and technologies were used in this project:

- [Gradle Build Tool](https://gradle.org/)
- [Java 21](https://www.oracle.com/java/technologies/downloads/)
- [Montoya API](https://portswigger.github.io/burp-extensions-montoya-api/javadoc/burp/api/montoya/MontoyaApi.html)
- [Python's aiohttp](https://docs.aiohttp.org/en/stable/index.html)

## :hammer: Building ##

Before starting :checkered_flag:, you need to have [Git](https://git-scm.com) and [Java](https://www.oracle.com/java/) installed.

```bash
# Clone this project
$ git clone https://github.com/y0k4i-1337/copy-as-python-aiohttp

# Access
$ cd copy-as-python-aiohttp
```

For MacOS and Linux:

```bash
# Build the project
$ ./gradlew build
```

For Windows:

```bash
# Build the project
$ ./gradlew.bat build
```

Once built, you can find the resulting `jar` file at

```
./copy-as-aiohttp-extension/build/distributions/copy-as-python-aiohttp-MAJOR.MINOR.PATCH.jar
```

## :memo: License ##

This project is under license from MIT. For more details, see the [LICENSE](LICENSE.md) file.


Made with :heart: by <a href="https://github.com/y0k4i-1337" target="_blank">y0k4i</a>

&#xa0;

<a href="#top">Back to top</a>
