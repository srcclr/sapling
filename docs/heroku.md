# Deploying to Heroku

Start by creating an app on Heroku and [installing the Heroku CLI](https://devcenter.heroku.com/articles/heroku-cli).

```sh
heroku authorizations:create
```

```
Creating OAuth Authorization... done
Client:      <none>
ID:          <id>
Description: Long-lived user authorization
Scope:       global
Token:       <token>
Updated at:  Sun Mar 15 2020 11:27:08 GMT+0800 (Singapore Standard Time) (less than a minute ago)
```

Create the database.

```sh
export HEROKU_API_KEY=<token>
heroku addons:create heroku-postgresql:hobby-dev -a sapling-planning
```

```
Creating heroku-postgresql:hobby-dev on ⬢ sapling-planning... free
Database has been created and is available
! This database is empty. If upgrading, you can transfer
! data from another database with pg:copy
Created <database> as DATABASE_URL
Use heroku addons:docs heroku-postgresql to view documentation
```

Initialize it:

```sh
heroku psql -a sapling-planning
```

```sql
CREATE SCHEMA agile AUTHORIZATION <username>;
ALTER ROLE <username> SET search_path TO agile;
```

Deploy and configure the server:

```sh
heroku config -a sapling-planning
```

```
=== sapling-planning Config Vars
DATABASE_URL: postgres://<username>:<password>@ec2-<ip>.compute-1.amazonaws.com:5432/<database>
```

Generate keypair for JWT token signing:

```sh
openssl genrsa -des3 -out private.pem 2048
openssl rsa -in private.pem -outform PEM -pubout -out public.pem

# Java requires keys in pkcs8 format
openssl pkcs8 -topk8 -inform PEM -outform PEM -in private.pem -out private-pkcs8.pem -nocrypt

heroku config:set PROPS_JWT_PRIVATE_KEY="$(cat private-pkcs8.pem)" PROPS_JWT_PUBLIC_KEY="$(cat public.pem)" -a sapling-planning
```

Stay within free tier memory quota:

```sh
heroku config:set JAVA_OPTS="-XX:+UseSerialGC -Xmx250M -Dserver.tomcat.max-threads=5" -a sapling-planning
```

Check out the [workflow configuration](../.github/workflows/main.yml) as well. You'll want `HEROKU_API_KEY` in the repository's [secrets](https://github.com/srcclr/sapling/settings/secrets).
