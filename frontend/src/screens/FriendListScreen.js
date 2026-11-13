import React, { useState, useEffect } from 'react';
import { 
  StyleSheet, Text, View, Image, TouchableOpacity, 
  SafeAreaView, FlatList, Platform, Alert, Modal, TextInput 
} from 'react-native';
import * as ImagePicker from 'expo-image-picker'; 
import { useNavigation, useIsFocused } from '@react-navigation/native';
import { getItem } from '../utils/authStorage'; 
import { API_URL } from '../constants/constants'; 

export default function FriendListScreen() {
  const navigation = useNavigation();
  const isFocused = useIsFocused();
  
  const [myInfo, setMyInfo] = useState(null);
  const [friendList, setFriendList] = useState([]);
  const [addModalVisible, setAddModalVisible] = useState(false);
  const [searchNickname, setSearchNickname] = useState("");
  // 에러 메시지 상태
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (isFocused) fetchData();
  }, [isFocused]);

  const fetchData = async () => {
    try {
      const token = await getItem('userToken');
      if (!token) return;
      const headers = { Authorization: `Bearer ${token}` };

      const userRes = await fetch(`${API_URL}/api/user/info`, { headers });
      const userJson = await userRes.json();
      if (userJson.success) setMyInfo(userJson.data);

      const friendRes = await fetch(`${API_URL}/api/friends`, { headers });
      const friendJson = await friendRes.json();
      if (friendJson.success) setFriendList(friendJson.data);

    } catch (e) { console.error(e); }
  };
  
  // 모달 초기화
  const handleCloseModal = () => {
      setAddModalVisible(false);
      setSearchNickname("");
      setErrorMessage("");
  };

  // 친구 추가 요청 로직 (웹/앱 분기 처리)
  const handleAddFriend = async () => {
    if (!searchNickname.trim()) { 
        setErrorMessage("닉네임을 입력해주세요."); 
        return; 
    }
    setErrorMessage("");
    
    const message = `${searchNickname}님에게 친구 요청을 보내시겠습니까?`;

    // 🌐 웹 환경 대응
    if (Platform.OS === 'web') {
        const ok = window.confirm(message);
        if (ok) {
            confirmAddFriend();
        }
    } else {
        // 📱 모바일 환경 대응
        Alert.alert(
            "친구 요청 확인",
            message,
            [
                { text: "취소", style: "cancel" },
                { 
                    text: "확인", 
                    onPress: confirmAddFriend, // 실제 API 호출
                }
            ]
        );
    }
  };
  
  // 실제 API 호출 및 에러 처리 함수
  const confirmAddFriend = async () => {
    try {
        const token = await getItem('userToken');
        const response = await fetch(`${API_URL}/api/friends`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
            body: JSON.stringify({ nickname: searchNickname })
        });
        
        const json = await response.json(); 

        if (json.success) {
            const successMsg = `${searchNickname}님에게 친구 요청을 보냈습니다.`;
            
            if (Platform.OS === 'web') {
                window.alert(successMsg);
            } else {
                Alert.alert("성공", successMsg);
            }
            
            handleCloseModal();
        } else { 
            // 서버에서 온 에러 메시지를 모달 내부에 표시
            const msg = json.message || "친구 요청에 실패했습니다. (알 수 없는 오류)";
            setErrorMessage(msg);
        }

    } catch (e) { 
        console.error("통신 중 심각한 오류 발생:", e); 
        setErrorMessage("서버와 통신하는 과정에서 문제가 발생했습니다.");
    }
  };

  const renderFriendItem = ({ item }) => (
    <TouchableOpacity 
      style={styles.friendItem}
      onPress={() => navigation.navigate('FriendProfile', { friend: item })} 
    >
      <Image 
        source={{ uri: item.profileImageUrl || "https://cdn-icons-png.flaticon.com/512/847/847969.png" }} 
        style={styles.friendImageSmall} 
      />
      <View style={styles.friendInfo}>
        <View style={styles.friendNameRow}>
            {item.representativeTitle ? (
                <Text style={styles.friendTitleSmall}>{item.representativeTitle}</Text>
            ) : null}
            <Text style={styles.friendName}>{item.friendNickname}</Text>
        </View>
        <Text style={styles.friendMessage} numberOfLines={1}>
            {item.statusMessage || "상태 메시지가 없습니다."}
        </Text>
      </View>
      <Text style={styles.arrowIcon}>{">"}</Text>
    </TouchableOpacity>
  );

  const pickImage = async () => {
    const { status } = await ImagePicker.requestMediaLibraryPermissionsAsync();
    if (status !== 'granted') { 
        const msg = '사진첩 접근 권한이 필요합니다.';
        if (Platform.OS === 'web') window.alert(msg); else Alert.alert('권한 부족', msg);
        return; 
    }
    let result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images, allowsEditing: true, aspect: [1, 1], quality: 0.5, base64: true,
    });
    if (!result.canceled) {
      const base64Img = `data:image/jpeg;base64,${result.assets[0].base64}`;
      updateProfileImageOnServer(base64Img);
    }
  };

  const updateProfileImageOnServer = async (base64Img) => {
    try {
        const token = await getItem('userToken');
        const response = await fetch(`${API_URL}/api/user/image`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
            body: JSON.stringify({ imageUrl: base64Img })
        });
        const json = await response.json();
        if (json.success) {
            setMyInfo({ ...myInfo, profileImageUrl: base64Img });
            const msg = "프로필 사진이 변경되었습니다!";
            if (Platform.OS === 'web') window.alert(msg); else Alert.alert("성공", msg);
        } else { 
            const msg = "사진 용량이 너무 큽니다.";
            if (Platform.OS === 'web') window.alert(msg); else Alert.alert("실패", msg);
        }
    } catch (e) { console.error(e); }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <TouchableOpacity onPress={() => navigation.goBack()} style={{padding: 10}}>
          <Text style={{fontSize: 24, color: '#A0C4FF'}}>{"<"}</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.addFriendBtn} onPress={() => setAddModalVisible(true)}>
            <Text style={styles.addFriendText}>친구 추가</Text>
        </TouchableOpacity>
      </View>

      <View style={styles.content}>
        <View style={styles.myProfileSection}>
            <TouchableOpacity onPress={pickImage} style={styles.myImageWrapper}>
                <Image 
                    source={{ uri: myInfo?.profileImageUrl || "https://cdn-icons-png.flaticon.com/512/3135/3135715.png" }} 
                    style={styles.myProfileImage} 
                />
                <View style={styles.cameraIconBadge}><Text>📷</Text></View>
            </TouchableOpacity>
            <Text style={styles.myNickname}>{myInfo?.nickname || "나"}</Text>
        </View>

        <View style={styles.divider} />

        <FlatList 
            data={friendList}
            renderItem={renderFriendItem}
            keyExtractor={item => item.id.toString()}
            style={styles.friendList}
            ListEmptyComponent={<Text style={{textAlign:'center', color:'#aaa', marginTop: 20}}>친구가 없습니다.</Text>}
        />
      </View>

      <Modal animationType="slide" transparent={true} visible={addModalVisible} onRequestClose={handleCloseModal}>
        <View style={styles.modalOverlay}>
            <View style={styles.modalContent}>
                <Text style={styles.modalTitle}>친구 추가</Text>
                
                <TextInput 
                    style={styles.input} 
                    placeholder="친구 닉네임 입력" 
                    value={searchNickname} 
                    onChangeText={(text) => {
                        setSearchNickname(text);
                        // 텍스트 변경 시 에러 메시지 초기화
                        if (errorMessage) setErrorMessage("");
                    }}
                />
                
                {/* 에러 메시지 영역 */}
                <View style={styles.errorContainer}>
                    <Text style={styles.errorText}>{errorMessage}</Text>
                </View>

                <View style={styles.modalBtnRow}>
                    <TouchableOpacity style={styles.cancelBtn} onPress={handleCloseModal}>
                        <Text style={styles.cancelText}>취소</Text>
                    </TouchableOpacity>
                    <TouchableOpacity style={styles.confirmBtn} onPress={handleAddFriend}>
                        <Text style={styles.confirmText}>추가</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </View>
      </Modal>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  header: { paddingHorizontal: 10, paddingTop: 10, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' },
  addFriendBtn: { borderWidth: 1, borderColor: '#eee', borderRadius: 15, paddingHorizontal: 12, paddingVertical: 6 },
  addFriendText: { fontSize: 13, color: '#555' },
  content: { flex: 1, alignItems: 'center', paddingHorizontal: 20 },
  myProfileSection: { alignItems: 'center', marginVertical: 20 },
  myImageWrapper: { position: 'relative' },
  myProfileImage: { width: 100, height: 100, borderRadius: 50, backgroundColor: '#eee' },
  cameraIconBadge: { position: 'absolute', bottom: 0, right: 0, backgroundColor: '#fff', borderRadius: 15, padding: 5, elevation: 3 },
  myNickname: { fontSize: 22, fontWeight: 'bold', marginTop: 10, color: '#333' },
  divider: { width: '100%', height: 1, backgroundColor: '#eee', marginVertical: 10 },
  friendList: { width: '100%' },
  friendItem: { flexDirection: 'row', alignItems: 'center', paddingVertical: 15, borderBottomWidth: 1, borderBottomColor: '#f0f0f0' },
  friendImageSmall: { width: 50, height: 50, borderRadius: 25, backgroundColor: '#eee', marginRight: 15 },
  friendInfo: { flex: 1 },
  friendNameRow: { flexDirection: 'row', alignItems: 'center', marginBottom: 4 },
  friendTitleSmall: { fontSize: 10, color: '#4A90E2', marginRight: 6, backgroundColor: '#F0F8FF', paddingHorizontal: 4, borderRadius: 4 },
  friendName: { fontSize: 16, fontWeight: 'bold', color: '#333' },
  friendMessage: { fontSize: 13, color: '#888' },
  arrowIcon: { fontSize: 18, color: '#ccc' },
  modalOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.3)', justifyContent: 'center', alignItems: 'center' },
  modalContent: { width: '80%', backgroundColor: 'white', borderRadius: 20, padding: 20, alignItems: 'center', elevation: 10 },
  modalTitle: { fontSize: 18, fontWeight: 'bold', marginBottom: 15 },
  input: { width: '100%', borderBottomWidth: 1, borderColor: '#ddd', padding: 10, fontSize: 16, marginBottom: 5 }, // margin bottom 감소
  
  // 에러 메시지 스타일
  errorContainer: { 
      width: '100%', 
      height: 20, 
      alignItems: 'center', 
      justifyContent: 'center', 
      marginBottom: 15 
  },
  errorText: { 
      color: '#EF5350', // 빨간색
      fontSize: 13, 
      fontWeight: 'bold' 
  },
  
  modalBtnRow: { flexDirection: 'row', width: '100%', justifyContent: 'space-between' },
  cancelBtn: { flex: 1, padding: 10, alignItems: 'center', marginRight: 5, backgroundColor: '#f0f0f0', borderRadius: 8 },
  confirmBtn: { flex: 1, padding: 10, alignItems: 'center', marginLeft: 5, backgroundColor: '#4A90E2', borderRadius: 8 },
  cancelText: { color: '#666' },
  confirmText: { color: 'white', fontWeight: 'bold' }
});