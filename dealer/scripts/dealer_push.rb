# gems
# activesupport
# speedy_gcm

#
# set API key and phone registration id
#

require 'active_support/core_ext'
require 'net/http'
require 'net/https'
require 'speedy_gcm'

# google console/api access
API_KEY = '...'

# phone registration id
PHONE_KEY = '...'

MESSAGE = {
  :registration_ids => [ PHONE_KEY ],
  :data => {
    :content => {
      :message => 'Booyah!'
    }
  }
}

SpeedyGCM::API.set_account API_KEY
response = SpeedyGCM::API.send_notification MESSAGE
puts "response: #{response.inspect}"
