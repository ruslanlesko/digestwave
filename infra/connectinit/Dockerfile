FROM python:3.9-slim-buster

COPY ./init.py /app/
WORKDIR /app
RUN pip install requests

CMD [ "python", "init.py" ]