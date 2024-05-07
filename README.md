# OpenuR Backend Server

## Table of Contents

- [Introduction](#introduction)
- [Development](#development)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Demonstration](#demonstration)
- [About](#about)

## Introduction

**OpenuR** 프로젝트의 자바 기반 스프링 부트를 활용한 백엔드 어플리케이션입니다. 

## Development

### Prerequisites

- JDK amazoncorretto 17
- Gradle 8.5
- MySQL 8.0.35

### Installation

#### 1. MySQL 설정

- 사용자 생성 및 Database 설정: 
- Database 및 테이블 설정: `src/main/resources/schema.sql` 참조

#### 2. config 파일 설정
`src/main/resources/application.yml.sample` 파일을 참조하여 `src/main/resources/application.yml` 파일 생성.

#### 3. 어플리케이션 빌드 및 실행
```bash
$ ./gradlew build
$ ./gradlew bootRun
```

### Demonstration
http://localhost:8080/swagger-ui/index.html

## About

Credit to @open-run/backend : @jryouda, @sonjh919, @yjkellyjoo and @DonHK-97
