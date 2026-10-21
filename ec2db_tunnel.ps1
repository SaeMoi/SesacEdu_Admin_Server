# 새싹에듀 EC2 ② DB 터널
# PC 의 localhost:1521 → (EC2 Instance Connect 엔드포인트, SSH 22) → EC2 ② → Oracle 172.31.128.252:1521
# 실행: powershell -ExecutionPolicy Bypass -File ec2db_tunnel.ps1     멈추기: Ctrl+C
# 전제: AWS CLI 2.32 이상, `aws login` 으로 개인 IAM 사용자(sesacedu-dev 그룹) 로그인

$Region     = "ap-northeast-2"
$InstanceId = "i-0c901ccf0ea1f3ef3"
$DbHost     = "172.31.128.252"
$LocalPort  = 1521
$KeyFile    = Join-Path $HOME ".ssh\sesacedu_tunnel_ed25519"
$KnownHosts = Join-Path $HOME ".ssh\sesacedu_known_hosts"

[Console]::OutputEncoding = [Text.Encoding]::UTF8
function Info($m) { Write-Host "[터널] $m" -ForegroundColor Cyan }
function Fail($m) { Write-Host "[터널] $m" -ForegroundColor Red; exit 1 }

# 1. 로그인 확인
Info "AWS 로그인 확인"
$who = aws sts get-caller-identity --query Arn --output text 2>$null
if (-not $who) { Fail "로그인 안 됨. 먼저 'aws login' 을 실행하세요." }
if ($who -like "*:root") { Fail "루트로 로그인돼 있습니다. 개인 IAM 사용자로 'aws login' 하세요." }
Info "로그인: $who"

# 2. 로컬 포트 확인
if (Get-NetTCPConnection -LocalPort $LocalPort -State Listen -ErrorAction SilentlyContinue) {
  Fail "localhost:$LocalPort 를 이미 쓰는 프로그램이 있습니다(이미 연 터널이나 로컬 Oracle). 닫고 다시 실행하세요."
}

# 3. 터널 전용 SSH 키 (처음 한 번 만들고 계속 씀)
if (-not (Test-Path $KeyFile)) {
  Info "터널용 SSH 키 생성: $KeyFile"
  New-Item -ItemType Directory -Force (Split-Path $KeyFile) | Out-Null
  ssh-keygen -t ed25519 -f $KeyFile -N '""' -q -C "sesacedu-tunnel-$env:USERNAME"
}
icacls $KeyFile /inheritance:r /grant:r "$($env:USERNAME):(R,W)" | Out-Null   # ssh 가 넓은 권한의 키를 거부하므로

# 4. 공개키를 서버에 60초 동안 등록 → 바로 접속
Info "임시 공개키 등록(60초 유효)"
$ok = aws ec2-instance-connect send-ssh-public-key --region $Region --instance-id $InstanceId `
  --instance-os-user ubuntu --ssh-public-key "file://$KeyFile.pub" --query Success --output text
if ($ok -ne "True") { Fail "공개키 등록 실패. IAM 사용자가 sesacedu-dev 그룹에 있는지 확인하세요." }

Info "터널 연결 중... 'localhost:$LocalPort' 로 DB 에 붙을 수 있습니다. 이 창을 닫거나 Ctrl+C 로 끊습니다(최대 1시간)."
ssh -F NUL -i $KeyFile -o IdentitiesOnly=yes `
  -o StrictHostKeyChecking=accept-new -o "UserKnownHostsFile=$KnownHosts" `
  -o "ProxyCommand=aws ec2-instance-connect open-tunnel --region $Region --instance-id $InstanceId" `
  -o ServerAliveInterval=30 -o ExitOnForwardFailure=yes `
  -N -L "127.0.0.1:${LocalPort}:${DbHost}:1521" "ubuntu@$InstanceId"
Info "터널이 닫혔습니다(종료 코드 $LASTEXITCODE). 다시 열려면 이 스크립트를 다시 실행하세요."
