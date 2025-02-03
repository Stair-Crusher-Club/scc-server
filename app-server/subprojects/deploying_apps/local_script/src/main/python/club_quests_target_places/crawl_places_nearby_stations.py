import csv
import requests

# API endpoint and authorization token
API_URL = "https://api.staircrusher.club/admin/places/startCrawling"
AUTH_TOKEN = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJfYiI6IlwiYWRtaW5cIiIsImlzcyI6Im91ci1tYXAtc2VydmVyIiwiZXhwIjoxNzYwNTQzODIwfQ.IkHTLjaKS8iuhTCuLgCBQ-Y2ClxthrjV4opanjrIoJK5yFFCFPFL_bWl7N--MoEIIzeWq0w5pHdWA91QM-kGfQ"
HEADERS = {
    "authorization": AUTH_TOKEN,
    "content-type": "application/json"
}

# Function to calculate boundary vertices for 250m squares within the given area
def calculate_boundary_vertices(lng, lat, distance_km=1):
    delta = distance_km / 111.32  # Approx. km per degree latitude
    eighth_delta = delta / 8

    # Define offsets for the 16 smaller 250m squares
    offsets = [
        (-3 * eighth_delta, -3 * eighth_delta), (-1 * eighth_delta, -3 * eighth_delta), (1 * eighth_delta, -3 * eighth_delta), (3 * eighth_delta, -3 * eighth_delta),
        (-3 * eighth_delta, -1 * eighth_delta), (-1 * eighth_delta, -1 * eighth_delta), (1 * eighth_delta, -1 * eighth_delta), (3 * eighth_delta, -1 * eighth_delta),
        (-3 * eighth_delta,  1 * eighth_delta), (-1 * eighth_delta,  1 * eighth_delta), (1 * eighth_delta,  1 * eighth_delta), (3 * eighth_delta,  1 * eighth_delta),
        (-3 * eighth_delta,  3 * eighth_delta), (-1 * eighth_delta,  3 * eighth_delta), (1 * eighth_delta,  3 * eighth_delta), (3 * eighth_delta,  3 * eighth_delta),
    ]

    # Generate boundary vertices for each square
    squares = []
    for offset_lng, offset_lat in offsets:
        square = [
            {"lng": lng + offset_lng - eighth_delta, "lat": lat + offset_lat - eighth_delta},
            {"lng": lng + offset_lng + eighth_delta, "lat": lat + offset_lat - eighth_delta},
            {"lng": lng + offset_lng + eighth_delta, "lat": lat + offset_lat + eighth_delta},
            {"lng": lng + offset_lng - eighth_delta, "lat": lat + offset_lat + eighth_delta},
            {"lng": lng + offset_lng - eighth_delta, "lat": lat + offset_lat - eighth_delta},
        ]
        squares.append(square)
    return squares

# Function to send a crawling request for a given set of boundary vertices
def send_crawling_request(boundary_vertices):
    payload = {"boundaryVertices": boundary_vertices}
    response = requests.post(API_URL, headers=HEADERS, json=payload)
    if response.status_code == 200:
        print("Crawling started successfully.")
    else:
        print(f"Failed to start crawling: {response.status_code}, {response.text}")

# Main function to process the CSV and initiate crawling
def main():
    input_csv = "subway_stations.csv"  # Replace with your CSV file name

    with open(input_csv, newline='', encoding='utf-8') as csvfile:
        reader = csv.DictReader(csvfile)
        for row in reader:
            station_name = row["지하철역"]
            longitude = float(row["lng"])
            latitude = float(row["lat"])

            print(f"Processing station: {station_name} ({longitude}, {latitude})")

            # Calculate boundary vertices for smaller squares
            squares = calculate_boundary_vertices(longitude, latitude, 1)  # 1 km as total area
            for square in squares:
                send_crawling_request(square)

if __name__ == "__main__":
    main()
