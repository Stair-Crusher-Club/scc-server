import csv

def generate_chunked_insert_queries(csv_file, table_name, chunk_size=1000):
    insert_queries = []
    values = []
    with open(csv_file, newline='', encoding='utf-8') as csvfile:
        separator = ',\n'
        reader = csv.DictReader(csvfile)
        for i, row in enumerate(reader, start=1):
            station_name = row['지하철역']
            lng = float(row['lng'])
            lat = float(row['lat'])

            # Prepare value for the query
            value = f"('{station_name}', {lng}, {lat}, ST_SetSRID(ST_MakePoint({lng}, {lat}), 4326))"
            values.append(value)

            # When chunk_size is reached, generate an INSERT query
            if i % chunk_size == 0:
                query = f"INSERT INTO {table_name} (name, lng, lat, geom) VALUES\n{separator.join(values)};"
                insert_queries.append(query)
                values = []  # Reset values for the next chunk

        # Add remaining rows as the final query
        if values:
            query = f"INSERT INTO {table_name} (name, lng, lat, geom) VALUES\n{separator.join(values)};"
            insert_queries.append(query)

    return insert_queries

if __name__ == "__main__":
    # Usage
    csv_file = "subway_stations.csv"  # Replace with your CSV file name
    table_name = "data_subway_station"    # Replace with your table name
    queries = generate_chunked_insert_queries(csv_file, table_name, chunk_size=1000)

    # Print queries
    for query in queries:
        print(query)
