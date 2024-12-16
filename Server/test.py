import requests

url = "http://localhost:5004/recommend"
data = {
    "username": "banapple87"
}

response = requests.post(url, json=data)

print("Status Code:", response.status_code)
print("Response JSON:", response.json())