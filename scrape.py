import requests
from bs4 import BeautifulSoup
import pandas as pd

url = "https://www.coingecko.com/?items=300/"

headers = {
    "User-Agent": "Mozilla/5.0"
}

response = requests.get(url, headers=headers)
soup = BeautifulSoup(response.text, "html.parser")

# Find the main market table
table = soup.find("table")

# Get headers
thead = table.find("thead").find_all("th")
headers = [th.text.strip() for th in thead]

# Get rows
rows = table.find("tbody").find_all("tr")

data = []

for row in rows:
    cols = row.find_all("td")
    row_data = [col.text.strip().replace("\n", " ") for col in cols]
    data.append(row_data)

# Create DataFrame
df = pd.DataFrame(data, columns=headers)

# Save to CSV
df.to_csv("coingecko_home_table.csv", index=False)

print("✅ CoinGecko homepage table scraped successfully")
print(df.head())












