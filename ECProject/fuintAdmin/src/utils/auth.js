import Cookies from 'js-cookie'

const TokenKey = 'Access-Token'
const UserKey = 'User-Id'

export function getToken() {
  return Cookies.get(TokenKey)
}

export function setToken(token) {
  return Cookies.set(TokenKey, token)
}

export function removeToken() {
  return Cookies.remove(TokenKey)
}

export function getUserId() {
  return Cookies.get(UserKey)
}

export function setUserId(userId) {
  return Cookies.set(UserKey, userId)
}

export function removeUserId() {
  return Cookies.remove(UserKey)
}

