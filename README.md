## Introduction

The repository is a part of Kare's management system framework, this framework is base of RBAC model, provides base information management, list below:

-   `Users management` manage users information
-   `Roles management` manage roles information
-   `Permissions management` all system's operation authorized by permission, the user who has a permission, it will has corrensbonding operation.
-   `Menus management` manage menus, you can configure different menu permissions for different user
-   `Databases management` manage backstage system table fields, you can configure display fields, editable fields and fields' related properties.
-   `Logs management` all request will be recorded, you can look or delete this logs.

The framework is divided to two parts, one is [KareAdminSite](https://github.com/Karelian-na/KareAdminSite), another is this repository that is the server of `KareAdminSite`, provides base management system api.

You can implementing more features base of this framework, more backstage features, or as an offical or other type websites's server.

## Technology stack
This repository using java programming language. web server framework using SpringBoot and MybatisPlus.

## Deployment
To run this server, first you should run the file [DataBase.sql](./src/DataBase.sql) to initialize the data. then you can run the server through your IDE, select KasApplication as main entry class to run.