import argparse
import csv
from cassandra.cluster import Cluster

def export_last_fetch_times(csv_file, cassandra_host):
    cluster = Cluster([cassandra_host])
    session = cluster.connect('digestwave')

    rows = session.execute("""
        SELECT uri, fetch_time FROM last_fetch_times
    """)

    with open(csv_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['uri', 'fetch_time'])

        for row in rows:
            writer.writerow([
                row.uri,
                row.fetch_time.isoformat() if row.fetch_time else ''
            ])

    print(f"✅ Exported to {csv_file}")
    cluster.shutdown()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Export last_fetch_times from Cassandra to CSV")
    parser.add_argument('--csv_file', required=True, help='Path to output CSV file')
    parser.add_argument('--host', default='127.0.0.1', help='Cassandra host (default: 127.0.0.1)')

    args = parser.parse_args()
    export_last_fetch_times(args.csv_file, args.host)
