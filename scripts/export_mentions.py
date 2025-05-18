import argparse
import csv
from cassandra.cluster import Cluster

def export_mentions(csv_file, cassandra_host):
    cluster = Cluster([cassandra_host])
    session = cluster.connect('digestwave')

    rows = session.execute("""
        SELECT keyword, published_at, article_title, article_url, sentiment
        FROM mentions
    """)

    with open(csv_file, 'w', newline='', encoding='utf-8') as f:
        writer = csv.writer(f)
        writer.writerow(['keyword', 'published_at', 'article_title', 'article_url', 'sentiment'])  # header

        for row in rows:
            writer.writerow([
                row.keyword,
                row.published_at.isoformat() if row.published_at else '',
                row.article_title,
                row.article_url,
                row.sentiment
            ])

    print(f"✅ Exported to {csv_file}")
    cluster.shutdown()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Export mentions table from Cassandra to CSV")
    parser.add_argument('--csv_file', required=True, help='Path to output CSV file')
    parser.add_argument('--host', default='127.0.0.1', help='Cassandra host (default: 127.0.0.1)')

    args = parser.parse_args()
    export_mentions(args.csv_file, args.host)
