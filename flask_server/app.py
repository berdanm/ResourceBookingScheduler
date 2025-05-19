from flask import Flask, request, jsonify
from flask_cors import CORS
import mysql.connector

app = Flask(__name__)
CORS(app)  # allow requests from your Android emulator

# MySQL configuration — adjust user/password/db as needed
db_config = {
    "host": "localhost",
    "user": "root",
    "password": "",
    "database": "booking_app"
}

# Helper to get a fresh DB connection
def get_db():
    return mysql.connector.connect(**db_config)

@app.route('/insert_booking', methods=['POST'])
def insert_booking():
    data = request.get_json()
    conn = get_db()
    cursor = conn.cursor()
    sql = """
        INSERT INTO bookings
          (resourceName, bookedBy, date, startTime, endTime)
        VALUES (%s, %s, %s, %s, %s)
    """
    vals = (
        data['resourceName'],
        data['bookedBy'],
        data['date'],
        data['startTime'],
        data['endTime']
    )
    cursor.execute(sql, vals)
    conn.commit()
    cursor.close()
    conn.close()
    return jsonify({"message": "Booking inserted"}), 201

@app.route('/get_bookings', methods=['GET'])
def get_bookings():
    conn = get_db()
    cursor = conn.cursor(dictionary=True)
    cursor.execute("SELECT * FROM bookings ORDER BY date, startTime")
    rows = cursor.fetchall()
    cursor.close()
    conn.close()
    return jsonify(rows), 200

@app.route('/delete_booking', methods=['POST'])
def delete_booking():
    data = request.get_json()
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("DELETE FROM bookings WHERE id = %s", (data['id'],))
    conn.commit()
    cursor.close()
    conn.close()
    return jsonify({"message": "Booking deleted"}), 200

@app.route('/update_booking', methods=['POST'])
def update_booking():
    data = request.get_json()
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        UPDATE bookings
        SET resourceName = %s,
            bookedBy     = %s,
            date         = %s,
            startTime    = %s,
            endTime      = %s
        WHERE id = %s
    """, (
        data['resourceName'],
        data['bookedBy'],
        data['date'],
        data['startTime'],
        data['endTime'],
        data['id']
    ))
    conn.commit()
    cursor.close()
    conn.close()
    return jsonify({"message": "Booking updated"}), 200

@app.route('/check_availability', methods=['POST'])
def check_availability():
    data = request.get_json()
    resource = data.get('resourceName')
    date_    = data.get('date')
    start    = data.get('startTime')
    end      = data.get('endTime')

    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("""
        SELECT COUNT(*) FROM bookings
        WHERE resourceName = %s
          AND date = %s
          AND NOT (endTime <= %s OR startTime >= %s)
    """, (resource, date_, start, end))
    (count,) = cursor.fetchone()
    cursor.close()
    conn.close()

    available = (count == 0)
    return jsonify({"available": available}), 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)
