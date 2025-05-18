import argparse
import csv
from cassandra.cluster import Cluster

def import_monthly_keywords(csv_file, cassandra_host):
    cluster = Cluster([cassandra_host])
    session = cluster.connect('digestwave')

    update_stmt = session.prepare("""
        UPDATE monthly_keywords SET count = count + ? WHERE month_year = ? AND keyword = ?
    """)

    with open(csv_file, newline='', encoding='utf-8') as f:
        reader = csv.DictReader(f)
        for row in reader:
            count = int(row['count'])
            month_year = row['month_year']
            keyword = row['keyword']
            if month_year and keyword and count != 0:
                session.execute(update_stmt, (count, month_year, keyword))

    print(f"✅ Imported from {csv_file}")
    cluster.shutdown()


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Import monthly_keywords from CSV into Cassandra (adds to counter)")
    parser.add_argument('--csv_file', required=True, help='Path to input CSV file')
    parser.add_argument('--host', default='127.0.0.1', help='Cassandra host (default: 127.0.0.1)')

    args = parser.parse_args()
    import_monthly_keywords(args.csv_file, args.host)
