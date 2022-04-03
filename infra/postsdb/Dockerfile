FROM postgres:14-bullseye
COPY ./001-setup.sh /docker-entrypoint-initdb.d/001-setup.sh
COPY ./002-init.sql /docker-entrypoint-initdb.d/002-init.sql

RUN apt update && apt -y install curl ca-certificates gnupg
RUN curl https://www.postgresql.org/media/keys/ACCC4CF8.asc | gpg --dearmor | tee /etc/apt/trusted.gpg.d/apt.postgresql.org.gpg >/dev/null
RUN apt update && apt -y install postgresql-14-cron

ENTRYPOINT ["docker-entrypoint.sh"]
EXPOSE 5432
CMD ["postgres"]
