import argparse
import csv
from cassandra.cluster import Cluster

def export_monthly_keywords(csv_file, cassandra_host):
    cluster = Cluster([cassandra_host])
    session = cluster.connect('digestwave')

    rows = session.execute("""
        SELECT month_year, keyword, count FROM monthly_keywords
    """)

    with open(csv_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['month_year', 'keyword', 'count'])

        for row in rows:
            writer.writerow([row.month_year, row.keyword, row.count])

    print(f"✅ Exported to {csv_file}")
    cluster.shutdown()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Export monthly_keywords table from Cassandra to CSV")
    parser.add_argument('--csv_file', required=True, help='Path to output CSV file')
    parser.add_argument('--host', default='127.0.0.1', help='Cassandra host (default: 127.0.0.1)')

    args = parser.parse_args()
    export_monthly_keywords(args.csv_file, args.host)
