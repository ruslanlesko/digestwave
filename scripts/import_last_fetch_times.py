import argparse
import csv
from cassandra.cluster import Cluster
from datetime import datetime

def import_last_fetch_times(csv_file, cassandra_host):
    cluster = Cluster([cassandra_host])
    session = cluster.connect('digestwave')

    upsert_stmt = session.prepare("""
        INSERT INTO last_fetch_times (uri, fetch_time)
        VALUES (?, ?)
    """)

    with open(csv_file, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            uri = row['uri']
            fetch_time_str = row['fetch_time']

            if not uri or not fetch_time_str:
                continue

            fetch_time = datetime.fromisoformat(fetch_time_str)
            session.execute(upsert_stmt, (uri, fetch_time))

    print(f"✅ Imported from {csv_file}")
    cluster.shutdown()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Import last_fetch_times into Cassandra from CSV")
    parser.add_argument('--csv_file', required=True, help='Path to input CSV file')
    parser.add_argument('--host', default='127.0.0.1', help='Cassandra host (default: 127.0.0.1)')

    args = parser.parse_args()
    import_last_fetch_times(args.csv_file, args.host)
