import argparse
import csv
from cassandra.cluster import Cluster
from datetime import datetime

def import_mentions(csv_file, cassandra_host):
    cluster = Cluster([cassandra_host])
    session = cluster.connect('digestwave')

    insert_stmt = session.prepare("""
        INSERT INTO mentions (keyword, published_at, article_title, article_url, sentiment)
        VALUES (?, ?, ?, ?, ?)
    """)

    with open(csv_file, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            # Skip rows with missing required fields
            if not row['keyword'] or not row['published_at']:
                continue

            session.execute(insert_stmt, (
                row['keyword'],
                datetime.fromisoformat(row['published_at']),
                row['article_title'],
                row['article_url'],
                int(row['sentiment']) if row['sentiment'] else None
            ))

    print(f"✅ Imported from {csv_file}")
    cluster.shutdown()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Import mentions from CSV into Cassandra")
    parser.add_argument('--csv_file', required=True, help='Path to input CSV file')
    parser.add_argument('--host', default='127.0.0.1', help='Cassandra host (default: 127.0.0.1)')

    args = parser.parse_args()
    import_mentions(args.csv_file, args.host)
