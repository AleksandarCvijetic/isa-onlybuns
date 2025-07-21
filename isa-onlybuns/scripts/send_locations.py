import pika
import json
import time

# Učitaj podatke
with open('locations.json', 'r', encoding='utf-8') as f:
    locations = json.load(f)

# RabbitMQ konekcija
connection = pika.BlockingConnection(pika.ConnectionParameters('localhost'))
channel = connection.channel()

# Kreiraj red
channel.queue_declare(queue='care_locations_queue', durable=True)

# Slanje svake 10 minuta
for location in locations:
    message = json.dumps(location)
    channel.basic_publish(exchange='',
                          routing_key='care_locations_queue',
                          body=message.encode('utf-8'))
    print(f"[x] Poslato: {message}")
    time.sleep(90) 