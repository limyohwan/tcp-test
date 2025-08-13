import socket
import threading

HOST = '127.0.0.1'
PORT = 9999

def parse_message(data):
    전문구분코드 = data[0:4].decode('ascii').strip()
    전문번호 = data[4:10].decode('ascii').strip()
    계좌번호 = data[10:25].decode('utf-8').strip()
    잔액 = "10000"
    예비영역 = ""
    
    return {
        '전문구분코드': 전문구분코드,
        '전문번호': 전문번호,
        '계좌번호': 계좌번호,
        '잔액': 잔액,
        '예비영역': 예비영역
    }

def create_response(parsed):
    res = bytearray()
    res.extend(parsed['전문구분코드'].encode('ascii'))
    res.extend(parsed['전문번호'].encode('ascii'))
    res.extend(parsed['계좌번호'].ljust(15).encode('utf-8'))
    res.extend(parsed['잔액'].ljust(15).encode('utf-8'))
    res.extend(parsed['예비영역'].ljust(10).encode('utf-8'))
    print(f"파싱된 데이터: {res}")
    return res

def handle_client(client_socket, addr):    
    try:
        while True:
            data = client_socket.recv(25)            
            if not data or len(data) < 25:
                break
                                    
            parsed = parse_message(data)
                            
            client_socket.send(create_response(parsed))
            
    except Exception as e:
        print(f"오류 발생: {e}")
    finally:
        client_socket.close()
        print(f"클라이언트 연결 종료: {addr}")

def start_server():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(5)
    
    try:
        while True:
            client, addr = server.accept()
            client_thread = threading.Thread(target=handle_client, args=(client, addr))
            client_thread.daemon = True
            client_thread.start()
    except KeyboardInterrupt:
        print("terminate server")
    finally:
        server.close()

if __name__ == "__main__":
    start_server()